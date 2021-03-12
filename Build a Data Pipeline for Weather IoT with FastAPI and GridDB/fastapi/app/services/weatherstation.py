from collections import OrderedDict
from datetime import datetime

import numpy as np


def get_sensor_data() -> OrderedDict:
    """Activates sensors and retrieves readings."""
    return OrderedDict({
        "timestamp": datetime.utcnow(),
        "temperature": 30 + np.random.standard_normal(),
        "pressure": 1005 + np.random.standard_normal() * 10,
        "humidity": np.random.randint(50, 99),
    })
