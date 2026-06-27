import * as dotenv from 'dotenv';
import readlineSync from 'readline-sync';
import { GoogleGenAI } from "@google/genai";
import { Pinecone } from '@pinecone-database/pinecone';
dotenv.config();

// Bug 1 fixed: removed duplicate `import { GoogleGenAI }` (was on line 5)
// Bug 2 fixed: added apiKey to the global ai instance
const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
const History = [];

async function chatting(question) {
    try {
        // Bug 3 fixed: removed local `const ai = new GoogleGenAI(...)` that was
        // shadowing the global `ai` — the global one is used for the LLM call below

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

        const context = searchResults.matches.map(match => match.metadata.text).join("\n\n---\n\n");

        History.push({
            role: 'user',                   // Bug 4 fixed: semicolon → comma
            parts: [{ text: question }]
        });

        // Bug 5 fixed: `ai.interactions.create` doesn't exist → use `ai.models.generateContent`
        const res = await ai.models.generateContent({
            model: "gemini-2.0-flash-lite",
            contents: History,
            config: {
                systemInstruction: `
                    You have to behave like a helpful assistant and answer the questions
                    based on the context you have got. You are a helpful assistant which takes input from the user about a YouTube video link. You get context regarding the question asked. If the answer is not in your context, let the user know politely.
                    Context:\n${context}
                `
            }
        });

        // Bug 6 fixed: was using `response.text` (the embedding response) → use `res.text`
        History.push({
            role: 'model',
            parts: [{ text: res.text }]
        });

        console.log("\n");
        console.log(res.text);
        console.log("\n");

    } catch (err) {
        console.log("Error in chatting:", err);
    }
}

async function main() {
    const userProblem = readlineSync.question("Enter your problem : ");
    await chatting(userProblem);
    main();
}

main();