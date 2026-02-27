import { defineCollection } from 'astro:content';
import { glob } from 'astro/loaders';
import { z } from 'astro/zod';

const projects = defineCollection({
    loader: glob({
        pattern: '**/*.md',
        base: './src/content/projects',
        generateId: ({ entry }) => entry.replace(/\.md$/, ''),
    }),
    schema: ({ image }) => z.object({
        title: z.string(),
        teamName: z.string(),
        description: z.string(),
        thumbnail: image(),
        type: z.string(),
        isFinalist: z.boolean()
    })
});

export const collections = { projects };