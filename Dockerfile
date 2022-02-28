# Jupiter docker image
#
# to use:
#
# 1. install docker, see docker.com
# 2. clone the git repo including this Dockerfile
# 3. build the container with ```docker build -t jupiter .```
# 4. run the created Gravity container with ```docker run -d -p 7876:7876 jupiter```


FROM openjdk:8
# start off with standard ubuntu images

WORKDIR /usr/jupiter

# Set local and enable UTF-8
ENV LANG C.UTF-8
ENV LANGUAGE C
ENV LC_ALL C.UTF-8

COPY . .

# compile Jupiter
RUN sed -i 's/\r//g' *.sh
RUN chmod +x compile.sh
RUN bash ./compile.sh

# make start script executable
RUN chmod +x run.sh

# both Jupiter ports get exposed
EXPOSE 6864 6876

CMD ./run.sh
