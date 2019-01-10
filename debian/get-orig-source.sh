#!/bin/bash

set -ex

PKG="${PACKAGE_NAME:-${1}}"
VERSION="${PACKAGE_VERSION:-${2}}"
ADD_PATCH="${3:-false}"
ZIPFILE="${PKG}-${VERSION}.zip"
ORIG_TARBALL="../${PKG}_${VERSION}.orig.tar.gz"

[ ! -f "${ORIG_TARBALL}" ] || exit 0

VER=$(echo "${VERSION}" | awk -F'.v' '{print $1}')

rm -rf jetty.project-*
rm -rf "${PKG}-${VER}"
rm -f "${ZIPFILE}"

wget -c "https://github.com/eclipse/jetty.project/archive/jetty-${VERSION}.zip" -O "${ZIPFILE}" || exit 1

unzip "${ZIPFILE}" || exit 1

rm -f "${ZIPFILE}"

mv jetty.project-* "${PKG}-${VER}"

rm -rf "${PKG}-${VER}"/example-*
rm -rf "${PKG}-${VER}"/test*
rm -rf "${PKG}-${VER}"/LICENSE-CONTRIBUTOR
rm -f "${PKG}-${VER}"/settings.xml
rm -f "${PKG}-${VER}"/.travis.yml
rm -f "${PKG}-${VER}"/jetty-continuation/src/main/java/org/eclipse/jetty/continuation/Jetty6Continuation.java

cp debian/libjetty8-java.pom.xml "${PKG}-${VER}"/pom.xml

find "${PKG}-${VER}" -type f -name '*.jpg' -exec rm -f '{}' \;
find "${PKG}-${VER}" -type f -name '*.java' -or -name '*.xml' -exec iconv -f ISO-8859-1 -t UTF-8 '{}' -o '{}'.iconv \; -exec mv '{}'.iconv '{}' \; -exec dos2unix '{}' \;

if [ "${ADD_PATCH}" != "false" ]
then
   while read -r line
   do
      patch -d "${PKG}-${VER}" -p1 < "debian/patches/${line}"
   done < debian/patches/series
fi

tar -czf "${ORIG_TARBALL}" --exclude-vcs "${PKG}-${VER}" || exit 1

rm -rf "${PKG}-${VER}"
rm -f "${ZIPFILE}"

