FROM centos:7

RUN yum -y groupinstall "Development Tools"
RUN yum -y install epel-release wget
RUN yum -y install python36 python36-devel 
RUN rpm -Uvh https://github.com/griddb/c_client/releases/download/v4.2.0/griddb_c_client-4.2.0-1.linux.x86_64.rpm
RUN ln -sf /usr/include/python3.6m /usr/include/python3.6

RUN wget https://sourceforge.net/projects/pcre/files/pcre/8.39/pcre-8.39.tar.gz
RUN tar xvfz pcre-8.39.tar.gz 
RUN cd pcre-8.39 && ./configure &&  make &&  make install
RUN cd ..

RUN wget https://prdownloads.sourceforge.net/swig/swig-3.0.12.tar.gz
RUN tar xvfz swig-3.0.12.tar.gz 
RUN cd swig-3.0.12 && ./configure &&  make && make install
RUN cd ..

RUN wget https://github.com/griddb/python_client/archive/0.8.1-rc0.tar.gz
RUN tar xvfz 0.8.1-rc0.tar.gz
RUN cd python_client-0.8.1-rc0 && make

ENV PYTHONPATH /python_client-0.8.1-rc0
ENV HOME /home/src
ENV VIRTUAL_ENV /opt/venv
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

WORKDIR $HOME

RUN python3 -m venv $VIRTUAL_ENV
RUN python -m pip install -U pip

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY app app
COPY init_griddb.py run.sh ./

ENV LC_ALL=en_US.utf-8
ENV LANG=en_US.utf-8

RUN chmod +x run.sh

CMD ./run.sh
