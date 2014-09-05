FROM ubuntu:trusty
MAINTAINER Andreas Simon <a.simon@quagilis.de>

# Install RabbitMQ
RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install --yes rabbitmq-server

# http://www.rabbitmq.com/configure.html#configuration-file
ADD rabbitmq.config /etc/rabbitmq/rabbitmq.config

EXPOSE 5672
EXPOSE 15672

CMD ["/usr/sbin/rabbitmq-server"]
