FROM azul/zulu-openjdk:8

RUN apt-get update \
    && apt-get install -y curl nano openssh-server openssh-client

RUN rm -f /etc/ssh/ssh_host_dsa_key /etc/ssh/ssh_host_rsa_key /root/.ssh/id_rsa \
    && ssh-keygen -q -N "" -t dsa -f /etc/ssh/ssh_host_dsa_key \
    && ssh-keygen -q -N "" -t rsa -f /etc/ssh/ssh_host_rsa_key \
    && ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa \
    && cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

ENV HADOOP_VERSION=3.3.1
ENV HADOOP_URL=https://downloads.apache.org/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz

RUN curl -fSL "$HADOOP_URL" -o /tmp/hadoop.tar.gz \
    && tar -xvf /tmp/hadoop.tar.gz -C /opt/ \
    && rm /tmp/hadoop.tar.gz

RUN ln -s /opt/hadoop-$HADOOP_VERSION /opt/hadoop \
    && mkdir /opt/hadoop/dfs \
    && mkdir /opt/hadoop/tmp \
    && ln -s /opt/hadoop-$HADOOP_VERSION/etc/hadoop /etc/hadoop

ADD core-site.xml /etc/hadoop/

ENV HADOOP_HOME /opt/hadoop
ENV HADOOP_CONF_DIR /etc/hadoop
ENV PATH $HADOOP_HOME/bin/:$PATH
ENV PATH $HADOOP_HOME/sbin/:$PATH
ENV JAVA_HOME /usr/lib/jvm/zulu8-ca-amd64