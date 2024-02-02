import React, { useState } from 'react';
import ImageUploader from './ImageUploader';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';

const App = () => {
    const [markdown, setMarkdown] = useState('');

    const handleMarkdownFetch = (markdownData) => {
        console.log(`markdownData:`)
        console.log(markdownData.result.choices[0].message.content);
        setMarkdown(markdownData.result.choices[0].message.content);
    };

    return (
    <div className="flex flex-col items-center justify-center min-h-screen space-y-4 bg-white">
        <h1 className="text-3xl">Extract Table From Image Using GPT4</h1>
        <div className="w-full max-w-4xl mx-auto py-12">
            <ImageUploader onMarkdownFetch={handleMarkdownFetch} />
            <div className="flex items-center justify-center overflow-x-auto w-full py-12">
                <ReactMarkdown className="markdown" remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeRaw]}>{markdown}</ReactMarkdown>
            </div>
        </div>
    </div>
);

};

export default App;
