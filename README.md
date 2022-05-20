# capi-wpt-querybot
This is a tool to query the content api, obtain a list of page url's and then return page load and performance information on those pages using the web page test api.
This project requires sbt to run.
The tool requires the use to provide a content api key, a webpage test api base url (the url of the webpagetest instance you will use) and a key for the webpagetest api.
an exampe config file has been provided to show the correct format - this is called "example.config".
At the moment the content api query is configured to get any liveblogs pulblished on the current day however this can be abstracted out to an input file if anyone wishes.
See [here](https://www.theguardian.com/info/developer-blog/2017/mar/06/empowering-our-editorial-teams-to-impact-page-performance) for more details about the tool.
