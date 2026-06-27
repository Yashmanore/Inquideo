import { YoutubeTranscript } from 'youtube-transcript';
import { GoogleGenerativeAIEmbeddings } from "@langchain/google-genai";
import { Pinecone } from '@pinecone-database/pinecone';
import { GoogleGenAI } from "@google/genai";
import { PineconeStore } from '@langchain/pinecone';
import { Document } from "@langchain/core/documents";
import readlineSync from 'readline-sync';
import { error } from 'console';
import * as dotenv from "dotenv";
dotenv.config();


const CHUNK_WINDOW_MS = 30000;
const OVERLAP_MS = 5000;
const STEP_MS = CHUNK_WINDOW_MS - OVERLAP_MS;

// Custom wrapper to support outputDimensionality (not natively supported by the base class)
class CustomGoogleGenerativeAIEmbeddings extends GoogleGenerativeAIEmbeddings {
    outputDimensionality;

    constructor(fields) {
        super(fields);
        this.outputDimensionality = fields?.outputDimensionality;
    }

    _convertToContent(text) {
        const req = super._convertToContent(text);
        if (this.outputDimensionality) {
            req.outputDimensionality = this.outputDimensionality;
        }
        return req;
    }
}

async function convertIntoVector(chunkedDocs) {
    // 1. Filter out empty chunks (to avoid storing empty text in Pinecone)
    const validChunks = chunkedDocs.filter(
        doc => doc.pageContent && doc.pageContent.trim().length > 0
    );

    if (validChunks.length === 0) {
        console.log("No valid chunks found.");
        return;
    }

    try {
        console.log(`Uploading ${validChunks.length} chunks to Pinecone...`);

        // 2. Set up the embeddings model with the correct model name and output dimensions
        const embeddingsModel = new CustomGoogleGenerativeAIEmbeddings({
            model: "gemini-embedding-001",
            taskType: "RETRIEVAL_DOCUMENT",
            apiKey: process.env.GEMINI_API_KEY,
            outputDimensionality: 768  // Must match your Pinecone index dimension
        });

        const pinecone = new Pinecone({
            apiKey: process.env.PINECONE_API_KEY
        });
        const pineconeIndex = pinecone.Index(process.env.PINECONE_INDEX_NAME);

        // 3. Store the filtered validChunks into Pinecone
        await PineconeStore.fromDocuments(validChunks, embeddingsModel, {
            pineconeIndex: pineconeIndex,
            maxConcurrency: 2  // Reduce to 1 if you hit 429 Rate Limit errors
        });

        console.log("✅ Embeddings successfully saved to Pinecone!");

    } catch (err) {
        console.error("❌ Error occurred:", err.message);
    }
}

function chunkTranscriptWithOverlap(rawTranscript) {
    try {
        if (!rawTranscript || rawTranscript.length === 0) return [];
        const documents = [];
        const videoEndMs = rawTranscript[rawTranscript.length - 1].offset + rawTranscript[rawTranscript.length - 1].duration;
        let chunkStartTime = rawTranscript[0].offset;
        while (chunkStartTime < videoEndMs) {
            const chunkEndTime = chunkStartTime + CHUNK_WINDOW_MS;
            const itemsInWindow = rawTranscript.filter(item =>
                item.offset >= chunkStartTime && item.offset < chunkEndTime
            );
            const cleanedTextArray = itemsInWindow
                .filter(item => !item.text.startsWith('[') && !item.text.endsWith(']'))
                .map(item => item.text.trim());
            if (cleanedTextArray.length > 0) {
                documents.push(new Document({
                    pageContent: cleanedTextArray.join(' '),
                    metadata: {
                        startTime: chunkStartTime / 1000,
                        endTime: Math.min(chunkEndTime, videoEndMs) / 1000
                    }
                }));
            }

            chunkStartTime += STEP_MS;
        }

        return documents;
    } catch (err) {
        console.log("Error in chunkTranscriptWithOverlap:", err);
    }
}

export async function loadTranscript() {
    try {
        const videoId = readlineSync.question('Enter YouTube Video ID or URL: ');

        // const ai = new GoogleGenAI({
        //     apiKey: process.env.GEMINI_API_KEY,
        // });

        if (!videoId.trim()) return;

        const rawTranscript = await YoutubeTranscript.fetchTranscript(videoId);
        const chunkedDocs = chunkTranscriptWithOverlap(rawTranscript);

        console.log("Number of chunks:", chunkedDocs.length);
        console.log("First chunk:", chunkedDocs[0]);

        // const embeddings = await ai.models.embedContent({
        //     model: "gemini-embedding-001",
        //     contents: chunkedDocs[0].pageContent
        // });

        // console.log(embeddings);

        await convertIntoVector(chunkedDocs);

        return chunkedDocs;

        // console.log("Length:", rawTranscript.length);

    } catch (err) {
        console.error(err);
    }
}