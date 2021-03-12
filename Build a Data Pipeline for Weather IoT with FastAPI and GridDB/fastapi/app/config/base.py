import os


GRIDDB_CONNECTION_PARAMS = {
    "notification_member": os.environ.get(
        "GRIDDB_NOTIFICATION_MEMBER",
        "griddb-server:10001"
    ),
    "cluster_name": os.environ.get("GRIDDB_CLUSTER_NAME", "defaultCluster"),
    "username": os.environ.get("GRIDDB_USERNAME", "admin"),
    "password": os.environ.get("GRIDDB_PASSWORD", "admin"),
}

GRIDDB_CONTAINER_NAME = os.environ.get(
    "GRIDDB_CONTAINER_NAME",
    "weatherstation"
)
