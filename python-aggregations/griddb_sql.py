import jpype
import jpype.dbapi2
import griddb_python as griddb
import sys
import os
import statistics

try:
    jpype.startJVM(classpath=["./lib/gridstore.jar", "./lib/gridstore-arrow.jar", "./lib/gridstore-jdbc.jar"])
except OSError:
    print("JVM already started.")


class GridDBJdbc:

    def __init__(self):
        """
        Initializes the GridDB connection store.
        """
        self.conn = None
        
        try:
            notification_provider = os.getenv("GRIDDB_NOTIFICATION_PROVIDER")
            cluster_name = os.getenv("GRIDDB_CLUSTER_NAME")
            username = os.getenv("GRIDDB_USERNAME")
            password = os.getenv("GRIDDB_PASSWORD")
            database = os.getenv("GRIDDB_DATABASE", "public") 
            
            jdbcUrl = "jdbc:gs:///" + cluster_name + "/"+ database + "?notificationProvider=" + notification_provider

            self.conn = jpype.dbapi2.connect(jdbcUrl, driver="com.toshiba.mwcloud.gs.sql.Driver",
                                        driver_args={"user":username, "password":password})

            
            print("Successfully connected to GridDB.")

            
        except griddb.GSException as e:
            print("Could not connect to GridDB, exiting.")
            print(f"GridDB Error: {e}")
            exit(-1)
        except Exception as e:
            print("An unexpected error occurred during GridDB connection.")
            print(f"Error: {e}")
            exit(-1)

    def calculate_avg(self):
        try:

            curs = self.conn.cursor()
            queryStr = 'SELECT temperature, humidity, pressure FROM telemetryData where TS BETWEEN TIMESTAMP_ADD(HOUR, NOW(), -1) AND NOW();'
            curs.execute(queryStr)
            if curs.description is None:
                print("Query returned no results or failed.")
                return None
            
            column_names = [desc[0] for desc in curs.description]
            all_rows = curs.fetchall()
            if not all_rows:
                print("No data found for the query range.")
                return None

            results = {name.lower(): [] for name in column_names}

            for row in all_rows:
                for i, name in enumerate(column_names):
                    results[name.lower()].append(row[i])

            averages = {
                'temperature': statistics.mean(results['temperature']),
                'humidity': statistics.mean(results['humidity']),
                'pressure': statistics.mean(results['pressure'])
            }

            return averages


        except jpype.JException as e:
            print(f"JPype/JDBC Error during query execution: {e}")
            return None
        except Exception as e:
            print(f"Unexpected Python Error: {e}")
            return None