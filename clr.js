import * as dotenv from "dotenv";
import { Pinecone } from "@pinecone-database/pinecone";

dotenv.config();

const pinecone = new Pinecone({
    apiKey: process.env.PINECONE_API_KEY,
});

export async function clearDatabase() {
    try {
        const index = pinecone.Index(process.env.PINECONE_INDEX_NAME);

        // In Pinecone SDK v5, deleteMany({ deleteAll: true }) on the index
        // object is silently ignored. You must target the namespace explicitly.
        // LangChain's PineconeStore writes to the default namespace ("") by default.
        await index.namespace('').deleteAll();

        console.log("✅ All vectors deleted successfully");
    } catch (err) {
        console.error("❌ Error clearing database:", err);
    }
}