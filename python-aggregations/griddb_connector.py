import os
import jpype
import griddb_python as griddb
from datetime import datetime
import sys

blob = bytearray([65, 66, 67, 68, 69, 70, 71, 72, 73, 74])
update = True

try:
    jpype.startJVM(classpath=["./lib/gridstore.jar", "./lib/gridstore-arrow.jar", "./lib/arrow-memory-netty.jar"])
except OSError:
    print("JVM already started.")

class GridDB: 
    factory = griddb.StoreFactory.get_instance()

    def __init__(self):
        # 1. Read all configuration from environment variables
        #    .get() safely returns 'None' if the variable is not set.
        self.notification_provider = os.environ.get('GRIDDB_NOTIFICATION_PROVIDER')
        self.cluster_name = os.environ.get('GRIDDB_CLUSTER_NAME')
        self.username = os.environ.get('GRIDDB_USERNAME')
        self.password = os.environ.get('GRIDDB_PASSWORD')
        self.database = os.environ.get('GRIDDB_DATABASE', 'public') # Default to 'public' if not set

        # 2. Validate that critical variables were actually found
        critical_vars = {
            "GRIDDB_NOTIFICATION_PROVIDER": self.notification_provider,
            "GRIDDB_CLUSTER_NAME": self.cluster_name,
            "GRIDDB_USERNAME": self.username,
            "GRIDDB_PASSWORD": self.password,
            "GRIDDB_DATABASE": self.database
        }
        
        missing_vars = [key for key, value in critical_vars.items() if value is None]

        if missing_vars:
            # If any critical var is missing, stop and raise an error
            raise EnvironmentError(f"Missing required environment variables: {', '.join(missing_vars)}")


        self.store = None

        try:
            self.store = GridDB.factory.get_store(
                notification_provider=self.notification_provider,
                cluster_name=self.cluster_name,
                username=self.username,
                password=self.password,
                database=self.database
            )
            print(f"Successfully connected to {self.cluster_name}.")
        except Exception as e:
            print(f"Failed to connect to GridDB: {e}")

    def get_store(self):
        return self.store
    
    def pushAvg(self, avg):
        try:
            avg_container = self.store.get_container("telemetry_hourly_summary")
            now = datetime.utcnow()
            ts_hour = now.replace(minute=0, second=0, microsecond=0)
            row = [ts_hour, avg["temperature"], avg["humidity"], avg["pressure"]]
            avg_container.put(row)
            print("Successfully inserted into telemetry_hourly_summary")
        except griddb.GSException as e:
            print("Error putting TimeSeries data:")
            for i in range(e.get_error_stack_size()):
                print(f"  [{i}] {e.get_error_code(i)}: {e.get_message(i)}")

