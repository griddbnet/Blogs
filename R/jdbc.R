library(RJDBC)
library(RCurl)

drv <- JDBC("com.toshiba.mwcloud.gs.sql.Driver",
            "../lib/gridstore-jdbc-5.2.0.jar",
            identifier.quote = "`")


url <- "https://dbaasshareextconsta.blob.core.windows.net/dbaas-share-extcon-blob/trial1602.json?sv=2015-04-05&sr=b&st=2023-03-14T00%3A00%3A00.00Z&se=2073-03-14T00%3A00%3A00.0000000Z&sp=r&sig=h2VJ0xAqsnfdgfgdfgRsqWVgfgfg5CAS66RifPIZ1PDCJ0x%2FiXb2FOhA%3D"
provider_encode <- curlEscape(url)

cluster_name <- "gs_clustertrial1602"
cn_encode <- curlEscape(cluster_name)

database_name <- "public"
dbn_encode <- curlEscape(database_name)

sslMode <- "&sslMode=VERIFY"
sm_encode <- curlEscape(sslMode)

username <- "israel"
password <- "israel"

protocol <- "jdbc:gs:///"

jdbc_url <- paste(protocol, cn_encode, "/", dbn_encode, "?notificationProvider=", provider_encode, sm_encode, sep="",collapse=NULL)


conn <- dbConnect(drv, jdbc_url, username, password, connectionRoute="PUBLIC")

rs <- dbGetQuery(conn, "SELECT * from Sample where ID > 2")

print(rs)
