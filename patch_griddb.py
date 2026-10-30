#!/usr/bin/env python3
"""Add GridDB and GridDB Cloud to Chat2DB Community's generic plugin jar.

Usage:
    python3 patch_griddb.py <original-generic-jar> <output-jar>

Reads generic.json from inside the jar, appends a GRIDDB entry (self-hosted,
FIXED_LIST) and a GRIDDB_CLOUD entry (notification provider + SSL), skipping
any that are already there, and writes a new jar. The original jar is never
modified.

Driver jars go in Chat2DB's jdbc-lib folder:
    GridDB:        jdbc-lib/gridstore-jdbc-5.8.0.jar
    GridDB Cloud:  jdbc-lib/griddb-cloud/gridstore.jar
                   jdbc-lib/griddb-cloud/gridstore-jdbc.jar
                   jdbc-lib/griddb-cloud/gridstore-advanced.jar
    (the Cloud jars come from the GridDB Cloud library download; rename them
    to drop the version numbers)
"""
import json
import sys
import zipfile

CONFIG_PATH = "ai/chat2db/plugin/generic/generic.json"
DRIVER_CLASS = "com.toshiba.mwcloud.gs.sql.Driver"

ENTRIES = [
    {
        "dbType": "GRIDDB",
        "name": "GridDB",
        "supportDatabase": False,
        "supportSchema": False,
        "tableQualification": "table",
        "driverConfigList": [
            {
                "defaultDriver": True,
                "custom": False,
                "downloadJdbcDriverUrls": [
                    "https://repo1.maven.org/maven2/com/github/griddb/gridstore-jdbc/5.8.0/gridstore-jdbc-5.8.0.jar"
                ],
                "jdbcDriver": "gridstore-jdbc-5.8.0.jar",
                "jdbcDriverClass": DRIVER_CLASS,
                "url": "jdbc:gs:///myCluster/public?notificationMember=localhost:20001",
            }
        ],
    },
    {
        "dbType": "GRIDDB_CLOUD",
        "name": "GridDB Cloud",
        "supportDatabase": False,
        "supportSchema": False,
        "tableQualification": "table",
        "driverConfigList": [
            {
                "defaultDriver": True,
                "custom": False,
                # No public download: the Cloud jars come from the portal.
                "downloadJdbcDriverUrls": [],
                # Comma-separated: Chat2DB loads all of them into one classloader.
                "jdbcDriver": "griddb-cloud/gridstore.jar,"
                              "griddb-cloud/gridstore-jdbc.jar,"
                              "griddb-cloud/gridstore-advanced.jar",
                "jdbcDriverClass": DRIVER_CLASS,
                "url": "jdbc:gs:///gs_clusterXXXX/DATABASE"
                       "?notificationProvider=URL_ENCODED_PROVIDER"
                       "&sslMode=PREFERRED&connectionRoute=PUBLIC",
            }
        ],
    },
]


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    src, dst = sys.argv[1], sys.argv[2]
    if src == dst:
        sys.exit("Output jar must be a different file from the original.")

    with zipfile.ZipFile(src) as zin:
        if CONFIG_PATH not in zin.namelist():
            sys.exit(f"{CONFIG_PATH} not found in {src}. Is this the generic plugin jar?")
        configs = json.loads(zin.read(CONFIG_PATH))
        # Only keep keys this jar's version already understands, so an older
        # jar doesn't choke on a field it has never seen.
        known = set().union(*(c.keys() for c in configs))
        existing = {c.get("dbType") for c in configs}

        for wanted in ENTRIES:
            if wanted["dbType"] in existing:
                print(f"{wanted['dbType']} already present, skipped.")
                continue
            entry = {k: v for k, v in wanted.items() if k in known}
            dropped = set(wanted) - set(entry)
            if dropped:
                print(f"Note: this jar doesn't use {sorted(dropped)}, skipped those keys.")
            configs.append(entry)
            print(f"Added {wanted['dbType']}.")

        new_json = json.dumps(configs, indent=2, ensure_ascii=False).encode("utf-8")
        with zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                data = new_json if item.filename == CONFIG_PATH else zin.read(item.filename)
                zout.writestr(item, data)

    print(f"Done: {dst} ({len(configs)} databases)")


if __name__ == "__main__":
    main()