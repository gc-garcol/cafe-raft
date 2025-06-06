# K8S

## Prerequisite (Local)

### Minikube

```shell
minikube start --addons=coredns
kubectl get pods -n kube-system -l k8s-app=kube-dns
kubectl get deployment -n kube-system
```

```shell
minikube addons enable ingress

# your ingress resources would be available at "127.0.0.1"
sudo minikube tunnel
```

## Run

```shell

# Prepare image
docker build -t thaivan/cafe-raft:<tab> .
docker push thaivan/cafe-raft:<tab>

# 
kubectl apply -f .

curl http://localhost/node-0/actuator/info
curl http://localhost/node-1/actuator/info
curl http://localhost/node-2/actuator/info

#
minikube dashboard
```

```shell
kubectl delete --all deployments -n default
# kubectl delete deployment --all -n default --grace-period=0 --force
# kubectl delete all --all -n default --grace-period=0 --force
```
