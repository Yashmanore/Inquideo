import * as dotenv from "dotenv";
import { Pinecone } from "@pinecone-database/pinecone";

dotenv.config();

const pinecone = new Pinecone({
    apiKey: process.env.PINECONE_API_KEY,
});

export async function clearDatabase() {
    try {
        const index = pinecone.Index(process.env.PINECONE_INDEX_NAME);

        // Fetch index stats to get a list of all existing namespaces
        const stats = await index.describeIndexStats();
        const namespaces = Object.keys(stats.namespaces || {});

        if (namespaces.length === 0) {
            console.log("ℹ️ No namespaces found in Pinecone. Database is already clear.");
            return;
        }

        console.log(`Found active namespaces: ${namespaces.join(', ')}`);

        for (const ns of namespaces) {
            console.log(`🗑️  Deleting all vectors in namespace: "${ns}"...`);
            await index.namespace(ns).deleteAll();
        }

        console.log("✅ All vectors cleared successfully from all namespaces");
    } catch (err) {
        console.error("❌ Error clearing database:", err);
    }
}

// Only run automatically when executed directly: `node clr.js`
// When imported by query.js, this block is skipped.
// Normalize both paths for cross-platform comparison (fixes Windows backslash issue)
const normalizeP = p => p.replace(/\\/g, '/').replace(/^\/([A-Za-z]:)/, '$1');
const isMain = normalizeP(process.argv[1]) === normalizeP(new URL(import.meta.url).pathname);
if (isMain) {
    clearDatabase();
}