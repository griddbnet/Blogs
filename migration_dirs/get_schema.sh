#!/bin/bash

psql -d postgres -X -A -t -c "
WITH column_details AS (
    -- First, get all the column info for each table
    SELECT 
        table_name, 
        json_agg(
            json_build_object(
                'column_name', column_name, 
                'data_type', udt_name, 
                'is_nullable', is_nullable,
                'column_default', column_default
            ) ORDER BY ordinal_position
        ) AS columns 
    FROM 
        information_schema.columns 
    WHERE 
        table_schema = 'public' 
    GROUP BY 
        table_name
),
primary_key_details AS (
    -- Next, find the primary key columns for each table
    SELECT 
        kcu.table_name, 
        json_agg(kcu.column_name) AS pk_columns
    FROM 
        information_schema.table_constraints AS tc 
    JOIN 
        information_schema.key_column_usage AS kcu 
        ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
    WHERE 
        tc.constraint_type = 'PRIMARY KEY' 
        AND tc.table_schema = 'public'
    GROUP BY 
        kcu.table_name
)
-- Finally, join them together
SELECT 
    json_object_agg(
        cd.table_name, 
        json_build_object(
            'primary_key', COALESCE(pkd.pk_columns, '[]'::json),
            'columns', cd.columns
        )
    )
FROM 
    column_details cd
LEFT JOIN 
    primary_key_details pkd ON cd.table_name = pkd.table_name;
" > schema.json