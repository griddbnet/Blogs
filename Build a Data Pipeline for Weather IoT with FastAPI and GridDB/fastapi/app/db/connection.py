import griddb_python as griddb

from .utils import handle_exceptions


class GridDBConnection:
    """Convenience class for managing GridDB connection."""

    def __init__(self, connection_params: dict) -> None:
        self.connection_params = connection_params
        self.container_name = None
        self.gridstore = None
        self.container = None

    @handle_exceptions
    def init(self, container_name: str = None) -> None:
        """Sets gridstore and container instance."""
        factory = griddb.StoreFactory.get_instance()
        self.gridstore = factory.get_store(**self.connection_params)
        if container_name is not None:
            self.container_name = container_name
            self.container = self.gridstore.get_container(self.container_name)

    @handle_exceptions
    def create_container(self, container_info: griddb.ContainerInfo) -> None:
        """Creates container with given container info."""
        self.container_name = container_info.name
        self.container = self.gridstore.put_container(container_info)

    @handle_exceptions
    def execute_and_fetch(self, query_stmt: str, as_dict: bool = False) -> list:
        """Executes query on `self.container` and returns results."""
        query = self.container.query(query_stmt)
        row_set = query.fetch()

        results = []
        columns = row_set.get_column_names()

        while row_set.has_next():
            row = row_set.next()

            if as_dict:
                row = dict((k, v) for k, v in zip(columns, row))

            results.append(row)

        return results

    def cleanup(self) -> None:
        """Closes container and store objects."""
        if self.container is not None:
            self.container.close()

        if self.gridstore is not None:
            self.gridstore.close()
