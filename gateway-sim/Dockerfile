FROM python:3.9

RUN pip install kafka-python

WORKDIR / 
ADD kafka_producer.py /

CMD /usr/local/bin/python /kafka_producer.py
