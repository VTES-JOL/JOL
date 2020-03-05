FROM tomcat:9-jdk13-openjdk-buster

ENV JOL_DATA /data
ENV JOL_VERSION docker-version
ENV JOL_RECAPTCHA_KEY 6LdpIycTAAAAAI8wDW6owPg5SbN24stPZA1iJpLq
ENV JOL_RECAPTCHA_SECRET 6LdpIycTAAAAAKa-sHrjSIOgyDDEysW8lCnTry9o

VOLUME /data

COPY target/jol.war /usr/local/tomcat/webapps