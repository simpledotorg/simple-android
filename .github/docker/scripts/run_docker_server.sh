###
### Hack to make it work with MIM attack from security ...
###
sed -i 's/https/http/g' Gemfile
gem sources -r https://rubygems.org/
yes | gem sources --add http://rubygems.org

###
### Downloads gems for test & development
###
bundle config --delete without
bundle install

###
### Cleans stuff if need be
###
rm -f tmp/pids/server.pid

###
### Initialises the DB
###
bundle exec rake db:setup


###
### Runs Simple
###
bundle exec rails s -p 3000 -b '0.0.0.0'
