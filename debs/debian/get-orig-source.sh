#!/bin/bash

set -ex

PKG="${1}"
VERSION="${2}"
ZIPFILE="${PKG}-${VERSION}.zip"
ORIG_TARBALL="../${PKG}_${VERSION}.orig.tar.xz"

[ ! -f "${ORIG_TARBALL}" ] || exit 0

rm -rf "jetty.project"*
rm -rf "${PKG}"*
rm -rf "${PKG}-${VERSION}"
rm -f "${ZIPFILE}"

wget -c -t 1 -T 5 "https://github.com/eclipse/jetty.project/archive/jetty-8.2.x.zip" -O "${ZIPFILE}" || exit 1

unzip "${ZIPFILE}" || exit 1

rm -f "${ZIPFILE}"

mv "jetty.project"* "${PKG}-${VERSION}"

find "${PKG}-${VERSION}" -type f -name '.classpath' -exec rm -f '{}' \;
find "${PKG}-${VERSION}" -type f -name '.project' -exec rm -f '{}' \;

tar -cJf "${ORIG_TARBALL}" --exclude-vcs "${PKG}-${VERSION}" || exit 1

rm -rf "${PKG}-${VERSION}"
rm -f "${ZIPFILE}"

