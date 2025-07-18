GitHub blog
- https://github.blog/

```plain
PowerShell (self)

$tools_home = "c:/dev/tools"
$git_home = "${tools_home}/git"

$env:Path = "${git_home}/bin;" + $env:Path
```

# rebase

```
git switch main
git switch --create b1
# ... make commits on b1
# ... meanwhile, new commits are added to main
git fetch origin
git rebase origin/main
```

# squash commits on the current branch

```
git rebase -i HEAD~n

or non-interactively

git reset --soft HEAD~n
git commit -m "..."

or

$parent_branch = "..."
# the base SHA (common ancestor)
$merge_base = git merge-base ${parent_branch} HEAD 
git reset --soft ${merge_base}
git commit -m "..."
```

# ?

```
"readme" | Out-File "readme4.md" 
```

# view per commit changes for a file

```
git log --patch -- path/to/file
```
'--' is not a switch, it is a separator that tells git, "everything after this is a file path, not a branch or other option"


# recover a deleted branch

When a branch is deleted, its commits may still be recoverable using ```git reflog```. Here's how to bring it back:

Option 1: find the sha of the tip of the branch at the time it was deleted

```
git log --walk-reflogs --oneline | Select-String "b1" -Context 0,1
```

We must switch to another branch before we can delete it. Look for first line from the top containing the branch name, ```...checkout: moving from b1 to ...```

The SHA of our branch tip will be the SHA in the line before this one, ```2782a87...commit...```

```
git switch --create b1 2782a87
```


# other bits

```
$env:my_name = "..."
$env:my_email = "..."
```

# Setup SSH keys
 
```
ssh-keygen -t ed25519 -C "$Env:my_email"
--> c:\Users\...\.ssh\bitbucket_id_ed25519
```

# Create config file

```
@"
Host hostname
  IdentityFile c:\Users\$Env:username\.ssh\bitbucket_id_ed25519
"@ > c:\Users\$Env:username\.ssh\config
```

Add bitbucket_id_ed25519.pub to Bitbucket SSH keys at repository site

# Configure Git
 
```
git config --global user.name "$Env:my_name"
git config --global user.email "$Env:my_email"
git config --global pull.ff only
git config --global alias.slog "log --graph --all --topo-order --decorate --oneline"
git config --global core.whitespace cr-at-eol
```

Git LFS issue cloning smarts-webserver
https://community.atlassian.com/t5/Bitbucket-articles/Git-LFS-v3-0-1-includes-backwards-incompatible-changes-with/ba-p/1819819
https://jira.atlassian.com/browse/BSERV-12989

(1) install latest Git from https://git-scm.com/download/win
- UNCHECK "Git LFS (Large File Support)"
- Use external OpenSSH
- Use the native Windows Securer Channel library
- Use Windows' default console window
- none (for credential helper)

(2) install Git LFS 2.13.3 from https://github.com/git-lfs/git-lfs/releases/tag/v2.13.3

(3) git lfs install

git lfs --version --> git-lfs/2.13.3 (GitHub; windows amd64; go 1.16.2; git a5e65851)


# Compare tip of branch with its fork

for the command below, we have checked out our feature branch
and we want to compare it with the fork point which was on branch ```ABC```

```
git diff $(git merge-base --fork-point ABC)
```
 
## SMARTS squash merge

- on feature branch
- see https://git-scm.com/docs/git-rebase#_interactive_mode

```
git rebase -i <oldest commit that you want to keep as is>

- on branch

git checkout <branch>
git fetch
git pull
git cherry-pick <commit> <commit> ...
git push
```

```git show <commit>```


# Git notes

- Git internals - see 
https://git-scm.com/book/en/v2/Git-Internals-Plumbing-and-Porcelain
https://www.freecodecamp.org/news/git-internals-objects-branches-create-repo/
- https://initialcommit.com/blog/git-notes#:~:text=Git%20notes%20are%20a%20way,without%20changing%20the%20commit%20itself
- https://git-scm.com/docs/git-notes

```
git fetch origin refs/notes/commits:refs/notes/commits
git notes add -m "this commit also applies to ..." commit-sha
git push origin refs/notes/commits
```         

For commits since a date

```
git log --author="ABC DEF" --since=2023-01-01 --oneline > c:\temp\commits.txt
```

quick way to copy repository to a new repository-x folder without having to git clone over slow network

```
create new folder, say, abc-2
copy .git folder from abc to abc-2

powershell as self to smarts-4

git checkout master
git reset --hard HEAD
```

visualizing-git - see https://git-school.github.io/visualizing-git/

# squash

```
git rebase -i HEAD~(number of commits)

git rebase -i (parent of the last commit you want to edit)
```

# rename a branch
```
git branch -m <old-name> <new-name>
``` 

# bisect

```
git bisect start
git bisect good
git bisect bad

git bisect reset
```

# something about finding a commit by some content??

```
this form has a : after the -L

git log -L:method-name:path/to/file
```
```
<start> and <end> are regex

- NO : after -L
- this form is useful if our method has anonymous inner classes
  such as actionPerformed

git log -L /start regex/,/end regex/:path/to/file
```
```
<start> and <end> are line numbers
- NO : after -L
- because <end> is not provided, we get 1-EOF

git log -L 1,:path/to/file
```

# create a new repository from the branch of an existing repository

cd ~\dev\projects

```
existing_repository_name="new-era"
existing_repository_copy="${existing_repository_name}_copy"
branch_name="java"
new_repository_name="java"

git clone git@github.com:jamie-burns0/${existing_repository_name}.git ${existing_repository_copy}

cd ${existing_repository_copy}

git checkout ${branch_name}
git remote add ${new_repository_name} git@github.com:jamie-burns0/${new_repository_name}.git

- create the new repository in GitHub

git push -u ${new_repository_name} ${branch_name}

- change the branch name in the new repository to main
- GitHub > new repository > Settings > Default Branch > main

cd ..

rm -rf ${existing_repository_copy}
```

# re-home a repository to an existing repository. For example, minmaxheap --> java

```
source_repository_name=java17-developer-certification
source_repository_name=minmaxheap
source_repository_name=springboot
source_repository_name=quicksort-go

destination_repository_name=java
destination_repository_name=go

cd ~/dev/projects

git clone git@github.com:jamie-burns0/${source_repository_name}.git ${source_repository_name}

cd ${source_repository_name}
mkdir ${source_repository_name}
ls -lart

- move everything to a folder so that there wont
  be any clash with files in the root of our destination

for f in item1 item2 ... ; do mv $f ${source_repository_name}; done

git add .
git commit -m "preparing to re-home ${source_repository_name} to ${destination_repository_name}"
git push

cd ..

git clone git@github.com:jamie-burns0/${destination_repository_name}.git ${destination_repository_name}

cd ${destination_repository_name}

git pull --all

git remote add ${source_repository_name} git@github.com:jamie-burns0/${source_repository_name}.git
git fetch ${source_repository_name}
git checkout main

git merge --allow-unrelated-histories ${source_repository_name}/main

git push origin main
```