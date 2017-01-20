
TeamCity Graphite Integration [![Build Status](https://travis-ci.org/mendhak/teamcity-graphite.svg?branch=master)](https://travis-ci.org/mendhak/teamcity-graphite)
===============

This TeamCity plugin will send build stats and metrics to Graphite.  You can send things such as `started`, `finished`, various code coverage stats, step durations, test metrics.  The actual metrics will vary depending on your build. This can also send FxCop and OpenCover metrics.



# Install

Download the [latest graphite.zip file](https://github.com/mendhak/teamcity-graphite/releases) and place it in the `<TeamCity data directory>/plugins` folder, then restart TeamCity.

Tested with TeamCity 8+.  If this works for you on an earlier version, please let me know.

# Set-up

Choose to add a build feature and from the dropdown, select `Send metrics to Graphite`

![step1](http://code.mendhak.com/teamcity-graphite/teamcity.graphite.1.png)


Fill in your Graphite server values and metric specs

![step2](http://code.mendhak.com/teamcity-graphite/teamcity.graphite.2.png)

For FxCop and OpenCover metrics, you will need to ensure that their resulting XML files are packaged into a resulting zip file as part of a build artifact. 

If you deploy using TeamCity, then the `started` and `finished` metrics will be useful for that particular build configuration, as you can use Graphite's [`drawAsInfinite`](http://graphite.readthedocs.org/en/1.0/functions.html#graphite.render.functions.drawAsInfinite) function.  

You can choose to whitelist branch names.  Specify a set of comma separated words.  If the branch name being built contains any of those words, the metrics will be reported to Graphite, else they will be discarded.  This is useful if you only want to report on develop, master and release branches but not feature branches.

# License

MIT


______________


# Development

Use `mvn package`, which produces the .zip file in `/target/graphite.zip`.  You can also use your favourite IDE's Maven integration to achieve the same.

# Troubleshooting

If the plugin doesn't seem to be working, you can find plugin messages in the log files under your TeamCity installation. (Examples: `/TeamCity/logs/teamcity-server.log`, `/TeamCity/logs/catalina.[DATE].log`)
This usually gives you a good idea of why a call may have failed.





