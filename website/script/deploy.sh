# bin/sh
ssh root@167.99.46.13 "rm -rf /home/nest2d-page/*"

scp -r dist/* root@167.99.46.13:/home/nest2d-page/
