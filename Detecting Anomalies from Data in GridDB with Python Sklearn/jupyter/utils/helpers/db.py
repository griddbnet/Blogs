import jaydebeapi


def get_connection() -> jaydebeapi.Connection:
    conn = jaydebeapi.connect(
        "com.toshiba.mwcloud.gs.sql.Driver",
        "jdbc:gs://griddb:20001/defaultCluster/public?notificationMember:127.0.0.1:20001",
        ["admin", "admin"],
        "/usr/share/java/gridstore-jdbc-4.5.0.jar",
    )
    return conn
