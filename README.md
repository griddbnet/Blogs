## Introduction

In this document, we'll first go over some notes about setting up GridDB and connecting to it from R. Then, we'll ingest some gene expression data from The Cancer Genome Atlas (TCGA), and query the GridDB backend using `dplyr` to generate simple summary statistics.

## Prerequisites

### Installing and starting GridDB

To install GridDB we simply follow the instructions provided in the [documentation](https://docs.griddb.net/latest/gettingstarted/using-apt/). On an Ubuntu machine, the easiest is to download the `deb` package and install with `dpkg -i`. Thereafter, we need to start the GridDB service with `sudo systemctl start gridstore`. After this, we are ready to connect to the service from R in order to load and query data. 

### Installing R dependencies

Running the code below would require installation (if not already available) of several R packages. Below, I am using the `attachment` package to detect the dependencies used by this Quarto markdown document. 

<div class="clipboard">
<pre><code class="language-r">attachment::att_from_rmd("01.Rmd")</code></pre>
</div>

One can install these dependencies via the usual `install.packages` method, except for `Bioconductor` packages that would require `BiocManager::install`:

<div class="clipboard">
<pre><code class="language-r">deps <- attachment::att_from_rmd("01.Rmd")
install.packages(deps)</code></pre>
</div>

or, in an `Ubuntu` environment (as it might be the case in a cloud setup), afaster approach might be to get pre-compiled versions using `Ubuntu`'s package manager:

<div class="clipboard">
<pre><code class="language-sh"># replace `PKG1/2/3` with the required package names
sudo apt install r-cran-PKG1 r-cran-PKG2 r-cran-PKG3 ...</code></pre>
</div>

## Connecting R to a local GridDB cluster

As documented [previously](https://griddb.net/en/blog/analysis-of-the-swisslos-lottery-using-r-and-griddb/), we'll use the `RJDBC` package (Java database connectivity) to connect to the GridDB cluster. As the cluster is running locally (as would be the case if you followed the installation and getting started [guide](https://docs.griddb.net/latest/gettingstarted/using-apt/#install-with-deb)), be sure to use the localhost IP address (`127.0.0.1`) and the port `20001`.

For ease of use down the line, we can wrap these two steps, creating a `jdbc` driver and a GridDB connection, in a function called `connect_to_griddb`. Note that we are loading the entire namespaces of `RJDBC` and `DBI`, which might be OK for an exploratory interactive session, but for more serious projects, we'd of course create an R package and import only the needed functions explicitly with `importFrom` or use an approach such as [`box`](https://github.com/klmr/box) to create modules with specific imports.

<div class="clipboard">
<pre><code class="language-r">suppressPackageStartupMessages({
  library(RJDBC)
  library(DBI)
  library(dplyr)
  library(tidyr)
  library(purrr)
  library(qs)
})
connect_to_griddb <- function() {
  drv <- JDBC(driverClass = "com.toshiba.mwcloud.gs.sql.Driver",
              # Point this to your gridstore jar
              classPath = "~/src/jdbc/bin/gridstore-jdbc.jar")
  
  conn <-
    dbConnect(drv,
              "jdbc:gs://127.0.0.1:20001/myCluster/public",
              "admin",
              "admin")
  
  return(conn)
}</code></pre>
</div>

## Data 

We'll work with some gene expression data from human cancer studies as published by The Cancer Genome Atlas (TCGA) [program](https://www.cancer.gov/about-nci/organization/ccg/research/structural-genomics/tcga). In particular, we'll focus on data packaged by the Bioconductor package [`GSEABenchmarkeR`](https://bioconductor.org/packages/devel/bioc/vignettes/GSEABenchmarkeR/inst/doc/GSEABenchmarkeR.html#setup) and intended to use for benchmarking gene-expression studies. The data were downloaded, pre-formatted into rectangular sets and saved as a [`qs`](https://github.com/traversc/qs) archive for faster saving and loading. 
  
A quick peak at the data sets:

<div class="clipboard">
<pre><code class="language-r">TCGA <- qs::qread("data/expressions.qs")
sapply(TCGA, nrow) |> summary()</code></pre>
</div>

We have expression data for 10 cancers, with a mean of 1.2M observations (rows). Each data set has 5 columns,

<div class="clipboard">
<pre><code class="language-r">TCGA[[1]] |> head() |> knitr::kable()</code></pre>
</div>

and the represented cancers are

<div class="clipboard">
<pre><code class="language-r">sapply(TCGA, "[[", 1, 5) |> unlist() |> knitr::kable()</code></pre>
</div>

## Loading our data into GridDB

To ingest the data, we first create a connection to the GridDB cluster using our function defined above. Then, we define two functions, one to create a table and another to insert data into the table. These functions will allow us to cycle overa list of data frames and load each data frame into the database seamlessly with base R's `*apply` family of functions or `purrr`'s `map_*` family of functions.

<div class="clipboard">
<pre><code class="language-r">griddb <- connect_to_griddb()
create_table <- function(table_name) {
  dbSendUpdate(
    griddb,
    sprintf(
      "CREATE TABLE IF NOT EXISTS %s (Gene STRING, Sample STRING, Expression INTEGER, Code STRING, Name STRING);",
      table_name
    )
  )
}
insert_table <- function(conn, name, df, append = TRUE) {
  for (i in seq_len(nrow(df))) {
    dbWriteTable(conn, name, df[i, ], append = append)
  }
}
# using `invisible` here to hide non-informative output
invisible(lapply(names(TCGA), create_table))
DBI::dbListTables(griddb)
# 6 minutes for 10K rows per table. So 6 min for 100K rows
# 1h for 100K rows per table. So 1h for 1M rows.
invisible(pbapply::pblapply(seq_along(TCGA), function(i)
  insert_table(
    conn = griddb,
    name = names(TCGA)[[i]],
    df = TCGA[[i]][1:100000, ],
    append = TRUE
  ))
)
DBI::dbDisconnect(griddb)</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-r">griddb <- connect_to_griddb()
iter <- setNames(names(TCGA), names(TCGA))
map_dfr(iter, function(x) {
  griddb |> tbl(x) |> collect() |> nrow()
}) |> knitr::kable()</code></pre>
</div>   

## Data analysis

Some summary simple stats. Nothing biologically meaningful at this point. The main idea is to use the familiar R packages like `dplyr`, `tidyr` and `purrr` toquery the fast GridDB backend database. The R code below, could well have been developed for a `MariaDB` or `PostgresSQL` database, but it will work equally well with a GridDB setup. 

### Genes per cancer dataset that have average expression > 2000 

<div class="clipboard">
<pre><code class="language-r">high_expression_genes <- purrr::map_dfr(iter, .id = "Cancer", 
  function(x) {
    griddb |>
      tbl(x) |>
      group_by(Gene) |>
      summarise(Mean_expression = mean(Expression, na.rm = TRUE)) |>
      filter(Mean_expression >= 2000) |> 
      collect()
  }
)
dim(high_expression_genes)
head(high_expression_genes) |> knitr::kable()</code></pre>
</div>

### Genes per cancer dataset that have average expression < 300 

<div class="clipboard">
<pre><code class="language-r">low_expression_genes <- purrr::map_dfr(iter, .id = "Cancer",
  function(x) {
    griddb |>
      tbl(x) |>
      group_by(Gene) |>
      summarise(Mean_expression = mean(Expression, na.rm = TRUE)) |>
      filter(Mean_expression <= 300) |> 
      collect() 
  }
)
dim(low_expression_genes)
tail(low_expression_genes) |> knitr::kable()</code></pre>
</div>

### Genes shared among all cancer data sets

<div class="clipboard">
<pre><code class="language-r">unique_genes_per_dataset <- purrr::map(iter, 
  function(x) {
    griddb |>
      tbl(x) |>
      collect() |>
      pull(Gene) |> 
      unique()
  }
)
shared_genes <- Reduce(intersect, unique_genes_per_dataset)
shared_genes</code></pre>
</div>

### Median expression per cancer data set of the first 10 genes shared for all data sets

<div class="clipboard">
<pre><code class="language-r">first10 <- shared_genes[1:10]
iter2 <- expand.grid(iter, first10, stringsAsFactors = FALSE)
first10_expression <-
  purrr::map2_dfr(iter2$Var1, iter2$Var2, function(.x, .y) {
    griddb |>
      tbl(.x) |>
      filter(Gene == .y) |> 
      collect() 
  })
first10_expression |>
  group_by(Code, Name, Gene) |>
  summarise(Mean_expression = mean(Expression, na.rm = TRUE)) |>
  ungroup() |> 
  select(-Name) |>
  pivot_wider(names_from = Code, values_from = Mean_expression) |> 
  knitr::kable()</code></pre>
</div>

## Clean up

<div class="clipboard">
<pre><code class="language-r">DBI::dbDisconnect(griddb)</code></pre>
</div>

## Conclusion

In this vignette, we saw how we can use GridDB database from R. We ingested one million rows of gene expression data in ten tables in about 1 hour and then queried the data seamlessly using familiar tools like `DBI` and `dplyr`. 

## Appendix

R / `Bioconductor` code used to download and preprocess the gene expression data.

<div class="clipboard">
<pre><code class="language-r"># This has a lot of dependencies and can take a few minutes to install
# BiocManager::install("GSEABenchmarkeR")
library(GSEABenchmarkeR)
tcga <- loadEData("tcga", nr.datasets = 10)
cancer_abbreviations <- names(tcga)
cancer_names <- c("BLCA" = "Bladder Urothelial Carcinoma",
                  "BRCA" = "Breast Invasive Carcinoma",
                  "COAD" = "Colon Adenocarcinoma",
                  "HNSC" = "Head and Neck Squamous Cell Carcinoma",
                  "KICH" = "Kidney Chromophobe",
                  "KIRC" = "Kidney Renal Clear Cell Carcinoma",
                  "KIRP" = "Kidney Renal Papillary Cell Carcinoma",
                  "LIHC" = "Liver Hepatocellular Carcinoma",
                  "LUAD" = "Lung Adenocarcinoma",
                  "LUSC" = "Lung Squamous Cell Carcinoma")
expressions <- lapply(seq_along(tcga), function(i) {
  tcga[[i]]@assays@data@listData |> 
    as.data.frame() |> 
    tibble::rownames_to_column(var = "Gene") |> 
    tidyr::pivot_longer(cols = -Gene, names_to = "Sample", values_to = "Expression") |> 
    mutate(cancer = cancer_abbreviations[[i]], cancer_name = cancer_names[[i]])
})
qs::qsave(expressions, "expressions.qs")</code></pre>
</div>

## Session info

<div class="clipboard">
<pre><code class="language-r">sessionInfo()</code></pre>
</div>
