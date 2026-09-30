# Red hat openshift notes

Red Hat developer sandbox

- https://sandbox.redhat.com/
- 30-day free trial

For openshift web console, 
  - click ```Try it``` button > select ```>_``` > ```oc whoami --show-console```

To install local CLI tools
  - web console > help > Command line tools

```
Powershell (self)

$env:Path += ";c:\dev\tools\oc"
```

For local CLI login,
  - web console > user profile dropdown > Copy login command
  - paste into powershell

## deploy quicksort-react on openshift

- if we run new-build more than once, we need to delete the buildconfig and imagestream it creates

```
oc delete is/quicksort-react
oc delete bc/quicksort-react
oc delete all -l app=quicksort-react
```

```
oc new-build --name=quicksort-react https://github.com/jamie-burns0/quicksort-react.git --context-dir=quicksort-react

oc logs -f bc/quicksort-react

- chooses nodejs:22-ubi9 builder
- creates is/quicksort-react

oc new-app --name=quicksort-react --image-stream=quicksort-react

oc logs -f deploy/quicksort-react

# have Next listen on port 8080 because the builder expects that port
oc set env deploy/quicksort-react PORT=8080

oc expose svc/quicksort-react

# test
oc rsh pod/quicksort-react-...
curl http://localhost:8080
exit

oc get route/quicksort-react
curl quicksort-react-jamie-burns0-dev...

in a browser navigate to
http://quicksort-react-jamie-burns0-dev...
```

or we could have built an image and deployed with

```
oc new-app --name=quicksort-react https://github.com/jamie-burns0/quicksort-react.git --context-dir=quicksort-react -e PORT=8080
```
