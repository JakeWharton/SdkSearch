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

FROM circleci/android:api-29

# Node for running webpack to build the production bundles and mocha for unit tests.
# Node 10 because the URL class was added as a global which is needed in unit tests.
# GraphViz is used to render the project graph.
RUN curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash - \
 && sudo apt-get install --no-install-recommends -y \
      nodejs \
      graphviz \
 && sudo rm -rf /var/lib/apt/lists/*
