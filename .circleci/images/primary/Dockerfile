# A custom container image for building SDK Search
#
# Build locally:
#   docker build .circleci/images/primary
#
# Test locally:
#   docker run -it <hash> /bin/bash
#
# Tag:
#   docker build -t jakewharton/sdksearch:<version> .circleci/images/primary
#
# Release:
#   docker push jakewharton/sdksearch:<version>

FROM circleci/android:api-28-alpha

RUN curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
RUN sudo apt-get install -y nodejs
