from typing import List, Optional

from pydantic import BaseModel


class ResponseModel(BaseModel):
    status: str = "ok"
    records: Optional[int] = 0
    data: Optional[List[dict]] = []
