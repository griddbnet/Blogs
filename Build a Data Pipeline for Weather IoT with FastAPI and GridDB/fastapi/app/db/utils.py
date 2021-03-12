from functools import wraps
from typing import Any, Callable

import griddb_python as griddb


def handle_exceptions(func: Callable) -> Callable:
    """Decorator that handles GridDB exceptions."""

    @wraps(func)
    def wrapper(*args, **kwargs) -> Any:
        try:
            return func(*args, **kwargs)

        except griddb.GSException as e:
            for i in range(e.get_error_stack_size()):
                print("[", i, "]")
                print(e.get_error_code(i))
                print(e.get_location(i))
                print(e.get_message(i))

    return wrapper
