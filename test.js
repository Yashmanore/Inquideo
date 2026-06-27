import { GoogleGenAI } from "@google/genai";
import dotenv from "dotenv";

dotenv.config();

const ai = new GoogleGenAI({
    apiKey: process.env.GEMINI_API_KEY,
});

async function main() {
    const response = await ai.models.embedContent({
        model: "gemini-embedding-001",
        contents: "What is the meaning of life?",
    });

    console.log(response);
}

main();