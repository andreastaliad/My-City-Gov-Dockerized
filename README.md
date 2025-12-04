
# My City Gov
My City Gov is your way to connect with all your goverment needs.




## Installation

### Run project with Docker:
Install Docker(Debian-Ubuntu):
```bash
  #Remove conflicting packages:
  sudo apt remove $(dpkg --get-selections docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc | cut -f1)
  # Add Docker's official GPG key:
  sudo apt update
  sudo apt install ca-certificates curl
  sudo install -m 0755 -d /etc/apt/keyrings
  sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  sudo chmod a+r /etc/apt/keyrings/docker.asc

  # Add the repository to Apt sources:
  sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
  Types: deb
  URIs: https://download.docker.com/linux/ubuntu
  Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
  Components: stable
  Signed-By: /etc/apt/keyrings/docker.asc
  EOF

  sudo apt update

  sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  
```
Run Docker Engine:
```bash
  sudo systemctl status docker
  #if already running no need to run again
  #else
  sudo systemctl start docker
```
Clone & run the app:
```bash
  git clone https://github.com/andreastaliad/My-City-Gov-Dockerized.git
  cd My-City-Gov-Dockerized
  docker compose up
```


    
## Related

Used for sms services:

[DS-Lab-NOC](https://github.com/gkoulis/DS-Lab-NOC)
