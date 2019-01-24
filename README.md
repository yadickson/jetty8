# Debian Package for Jetty8 Project

[![TravisCI Status][travis-image]][travis-url]

**Build dependencies**

- debhelper (>= 9)
- cdbs
- default-jdk
- maven-debian-helper (>= 1.5)
- maven-repo-helper
- ant-optional
- glassfish-jmac-api (>= 1:2.1.1-b31g-2)
- javahelper
- junit
- libbuild-helper-maven-plugin-java
- libgeronimo-jta-1.1-spec-java
- libmail-java
- libmaven-bundle-plugin-java
- libmaven-javadoc-plugin-java
- libmockito-java
- libservlet3.0-java (>= 7.0.40-2)

**Download source code**

- unzip
- wget
- libc-bin
- dos2unix 

```
$ debian/rules get-orig-source
$ debian/rules publish-source
```

**Build project**

```
$ dpkg-buildpackage -rfakeroot -D -us -uc -i -I -sa
```
or
```
$ QUILT_PATCHES=debian/patches quilt push -a
$ fakeroot debian/rules clean binary
```

**Tested**

- Debian stretch
- Debian buster

**Repositories**

[Debian repository](https://bintray.com/yadickson/debian)

```
$ wget -qO - https://bintray.com/user/downloadSubjectPublicKey?username=bintray | sudo apt-key add -
```
```
$ echo "deb https://dl.bintray.com/yadickson/debian [distribution] main" | sudo tee -a /etc/apt/sources.list.d/bintray.list
```
```
$ sudo apt-get update
$ sudo apt-get upgrade -y
$ sudo apt-get install libjetty8-java
```

## License

GPL-3.0 © [Yadickson Soto](https://github.com/yadickson)

Apache-2.0 EPL-1.0 © [Jetty Project](https://github.com/eclipse/jetty.project)

[travis-image]: https://api.travis-ci.org/yadickson/jetty8-debs.svg?branch=master
[travis-url]: https://travis-ci.org/yadickson/jetty8-debs

