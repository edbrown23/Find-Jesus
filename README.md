Find-Jesus
==========

A wikipedia web spider which looks for the shortest path to Jesus from a random page. Built with Selenium.

Current Developments
====================

- The web crawler is able to find the goal node, and construct a shortest path to that node
- The shortest path is not guaranteed to be the optimal path from start to finish. It is only the shortest path through the visited pages
- Nodes which are a part of the shortest path are weighted heavier
- Nodes visited during failed searches have their weights decreased, theoretically because they did not aid the search

Future Developments
===================

- Shortest path nodes often become so significantly favored that the crawler will choose them over an unknown, more optimal, path
- A custom tree implementation would likely be more conservative in memory usage, as JGraphT may be somewhat at fault for large memory use
- Cutting down on memory usage is of major importance
- The CPU is hardly utilized while crawling, as most of the time is spent loading each page. A better solution would be running many concurrent crawlers, and perhaps slowing each one to be nice
