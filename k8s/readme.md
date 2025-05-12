# K8S

## Prerequisite (Local)

### Minikube
```shell
minikube start --addons=coredns
kubectl get pods -n kube-system -l k8s-app=kube-dns
kubectl get deployment -n kube-system
```

## Run

### TLDR
```shell
kubectl apply -f .

minikube service node0-service --url
minikube service node1-service --url
minikube service node2-service --url
```

### SBS

```shell
# build image
docker build -t thaivan/cafe-raft:<tab> .
docker push thaivan/cafe-raft:<tab>

# service
kubectl apply -f 01-raft-nodeport-svc.yaml
kubectl get svc -o wide
## $ ps -ef | grep ssh
minikube service node0-service --url
minikube service node1-service --url
minikube service node2-service --url

# pod
kubectl apply -f 02-raft-deployment.yaml
kubectl get pods --show-labels

# inspect
kubectl logs -f pod/<pod-name>
kubectl describe pod <pod-name>
kubectl port-forward po/<pod-name> 8080:8080
kubectl exec -it <pod-name> -- /bin/sh
```

```shell
kubectl delete --all deployments -n default
# kubectl delete deployment --all -n default --grace-period=0 --force
# kubectl delete all --all -n default
```
