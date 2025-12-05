const { onDocumentWritten } = require('firebase-functions/v2/firestore');
const { onRequest } = require('firebase-functions/v2/https');
const { initializeApp } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');

initializeApp();

// automatically sync organizer status to auth custom claims when user document changes
exports.syncOrganizerClaim = onDocumentWritten(
    'users/{userId}',
    async (event) => {
        const userId = event.params.userId;
        const userData = event.data?.after?.data();

        try {
            if (userData) {
                await getAuth().setCustomUserClaims(userId, {
                    organizer: userData.organizer === true,
                });
                console.log(
                    `Set organizer claim for ${userId}: ${userData.organizer}`
                );
            } else {
                // User was deleted, remove claims
                await getAuth().setCustomUserClaims(userId, {
                    organizer: false,
                });
                console.log(
                    `Removed organizer claim for deleted user ${userId}`
                );
            }
        } catch (error) {
            console.error(`Error setting claims for ${userId}:`, error);
        }
    }
);

// Manual trigger to sync all existing users (call once after deployment)
exports.syncAllOrganizerClaims = onRequest(async (req, res) => {
    try {
        const usersSnapshot = await getFirestore().collection('users').get();

        const updates = usersSnapshot.docs.map(async (doc) => {
            const userData = doc.data();
            await getAuth().setCustomUserClaims(doc.id, {
                organizer: userData.organizer === true,
            });
            console.log(`Synced ${doc.id}: organizer=${userData.organizer}`);
        });

        await Promise.all(updates);
        res.send(`Successfully updated ${usersSnapshot.size} users`);
    } catch (error) {
        console.error('Error syncing claims:', error);
        res.status(500).send(`Error: ${error.message}`);
    }
});
