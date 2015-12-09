#!/usr/bin/python

import urllib
url = 'http://export.arxiv.org/api/query?search_query=all:electron&start=1000&max_results=2000'
data = urllib.urlopen(url).read()
#print data

f = open('workfile_run', 'w+')
f.write(data)
f.close()
