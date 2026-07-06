import * as dotenv from 'dotenv';
import readlineSync from 'readline-sync';
import { GoogleGenAI } from "@google/genai";
import { Pinecone } from '@pinecone-database/pinecone';
import { loadTranscript } from './index.js';
import { clearDatabase } from './clr.js';
dotenv.config();

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
const History = [];

async function chatting(question) {
    try {
        const response = await ai.models.embedContent({
            model: "gemini-embedding-001",
            contents: question,
            config: {
                outputDimensionality: 768,
                taskType: "RETRIEVAL_QUERY"
            }
        });

        const pinecone = new Pinecone({ apiKey: process.env.PINECONE_API_KEY });
        const pineconeIndex = pinecone.Index(process.env.PINECONE_INDEX_NAME);

        const searchResults = await pineconeIndex.query({
            topK: 5,
            vector: response.embeddings[0].values,
            includeMetadata: true,
        });

        const context = searchResults.matches.map(match =>
            `[${formatTime(match.metadata.startTime)}s - ${formatTime(match.metadata.endTime)}s]\n${match.metadata.text}`
        ).join("\n\n---\n\n");

        History.push({
            role: 'user',
            parts: [{ text: question }]
        });

        const res = await ai.models.generateContent({
            model: "gemini-2.5-flash",
            contents: History,
            config: {
                systemInstruction: `
                    You are a helpful assistant that answers questions about a YouTube video.
                    Answer based only on the context provided. Cite timestamps in [M:SS] format when relevant.
                    If the answer is not in the context, let the user know politely.
                    Context:\n${context}
                `
            }
        });

        History.push({
            role: 'model',
            parts: [{ text: res.text }]
        });

        console.log("\n" + res.text + "\n");

    } catch (err) {
        console.log("Error in chatting:", err);
    }
}

function formatTime(seconds) {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
}

async function main() {
    // Step 1: Ask for YouTube URL and load transcript into Pinecone
    console.log("\n🎬 YT-AI — YouTube RAG Chatbot\n");
    await loadTranscript();

    // Step 2: Chat loop
    console.log("\n✅ Video loaded! You can now ask questions. Type 'exit' to quit.\n");

    while (true) {
        const userQuestion = readlineSync.question("You: ");

        if (userQuestion.toLowerCase().trim() === "exit") {
            console.log("\n🗑️  Clearing Pinecone vectors...");
            await clearDatabase();
            console.log("👋 Goodbye!\n");
            break;
        }

        if (!userQuestion.trim()) continue;

        await chatting(userQuestion);
    }
}

main();