FROM hadoop-base:3.3.1

#HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 CMD curl -f http://localhost:50070/ || exit 1

ADD hdfs-site.xml /etc/hadoop/

RUN mkdir /opt/hadoop/dfs/name
VOLUME /opt/hadoop/dfs/name

RUN mkdir /opt/hadoop/dfs/data
VOLUME /opt/hadoop/dfs/data

ADD start.sh /start.sh
RUN chmod a+x /start.sh

EXPOSE 50070 9000 9866 8088 50075 50010

# CMD ["/start.sh", "/opt/hadoop/dfs/name"]
