docker compose -f ".github/docker/simple-server.compose.yml" up -d
docker compose -f ".github/docker/simple-server.compose.yml" exec server bash -c "RAILS_ENV=test bundle exec rails runner 'Flipper.enable :auto_approve_users; Flipper.enable :fixed_otp; puts \"Features enabled!\"'"
