library(RJDBC)
library(DBI)

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
}

griddb <- connect_to_griddb()



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
    dplyr::mutate(Code = cancer_abbreviations[[i]], Cancer = cancer_names[[i]])
})

names(expressions) <- cancer_abbreviations

qs::qsave(expressions, "expressions.qs")





