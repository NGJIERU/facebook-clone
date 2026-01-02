# 🚀 GitHub Setup Tutorial - Your First Push!

This guide will walk you through committing your code and pushing it to GitHub for the very first time.

---

## 📋 What We're Going to Do

1. ✅ Stage your files (prepare them for commit)
2. ✅ Commit files (save a snapshot with a message)
3. ✅ Create a GitHub repository
4. ✅ Connect your local project to GitHub
5. ✅ Push your code to GitHub

---

## Step 1: Understanding Git Status

```bash
git status
```

**What this shows:**
- **Untracked files** (in red): New files Git doesn't know about yet
- **Modified files** (in red): Files you've changed
- **Staged files** (in green): Files ready to be committed

**Current Status:**
You have 5 new documentation files that need to be added:
- .github/ (folder with CI/CD workflow)
- AWS_DEPLOYMENT.md
- CONTRIBUTING.md
- README.md
- SETUP.md

---

## Step 2: Stage Your Files

**What "staging" means:** You're telling Git "I want to include these files in my next snapshot (commit)."

```bash
# Add specific files
git add README.md
git add SETUP.md
git add AWS_DEPLOYMENT.md
git add CONTRIBUTING.md
git add .github/

# OR add everything at once
git add .
```

**After running this, check status again:**
```bash
git status
```

You should see the files in green now, meaning they're staged!

---

## Step 3: Commit Your Changes

**What a "commit" is:** A snapshot of your code at this point in time, with a message describing what changed.

```bash
git commit -m "docs: add comprehensive project documentation

- Add README.md with architecture diagrams and AWS deployment guide
- Add SETUP.md for GitHub beginners
- Add AWS_DEPLOYMENT.md with detailed cloud deployment steps
- Add CONTRIBUTING.md with coding standards
- Add CI/CD GitHub Actions workflow"
```

**Understanding the commit message:**
- First line: Brief summary (50 chars or less)
- Blank line
- Detailed description with bullet points

**After committing, check status:**
```bash
git status
# Should say: "nothing to commit, working tree clean"
```

---

## Step 4: Create a GitHub Repository

### Option A: Using GitHub Website (Recommended for Beginners)

1. **Go to GitHub:**
   - Visit https://github.com
   - Log in to your account

2. **Create New Repository:**
   - Click the "+" icon in top right
   - Select "New repository"

3. **Fill in Details:**
   - **Repository name:** `facebook-clone` (or any name you prefer)
   - **Description:** "Enterprise-grade social media platform with microservices architecture"
   - **Visibility:** 
     - Choose "Public" if you want anyone to see it
     - Choose "Private" if only you can see it
   - **Initialize repository:** 
     - ❌ **DO NOT** check "Add a README file"
     - ❌ **DO NOT** add .gitignore
     - ❌ **DO NOT** choose a license
     - (We already have these files!)

4. **Click "Create repository"**

5. **Copy the repository URL:**
   You'll see a page with setup instructions. Look for:
   ```
   https://github.com/YOUR_USERNAME/facebook-clone.git
   ```
   Copy this URL!

### Option B: Using GitHub CLI (Advanced)

```bash
# Install GitHub CLI first
brew install gh

# Login
gh auth login

# Create repository
gh repo create facebook-clone --public --source=. --remote=origin --push
```

---

## Step 5: Connect Local Project to GitHub

**What this does:** Links your local folder to the GitHub repository so you can push/pull code.

```bash
# Add the remote repository
git remote add origin https://github.com/YOUR_USERNAME/facebook-clone.git

# Verify it was added
git remote -v

# You should see:
# origin  https://github.com/YOUR_USERNAME/facebook-clone.git (fetch)
# origin  https://github.com/YOUR_USERNAME/facebook-clone.git (push)
```

**What is "origin"?**
- "origin" is the default name for your remote repository
- You can have multiple remotes, but "origin" is the main one

---

## Step 6: Push Your Code to GitHub

**What "push" means:** Upload your commits from your computer to GitHub.

```bash
# Push to GitHub (first time)
git push -u origin main

# What this does:
# - "push": Upload commits
# - "-u": Set upstream (remember this branch for future pushes)
# - "origin": The remote repository name
# - "main": The branch name
```

**If you get an error about authentication:**

You'll need to authenticate. GitHub has two methods:

### Method 1: Personal Access Token (Recommended)

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give it a name: "Facebook Clone Project"
4. Select scopes: Check "repo" (full control of private repositories)
5. Click "Generate token"
6. **COPY THE TOKEN NOW** (you won't see it again!)
7. When git asks for password, paste the token

### Method 2: SSH Key (More Secure, One-Time Setup)

```bash
# Generate SSH key
ssh-keygen -t ed25519 -C "your_email@example.com"
# Press Enter three times (default location, no passphrase)

# Copy the public key
cat ~/.ssh/id_ed25519.pub
# Copy the entire output

# Add to GitHub:
# 1. Go to GitHub → Settings → SSH and GPG keys
# 2. Click "New SSH key"
# 3. Paste the key
# 4. Click "Add SSH key"

# Change remote URL to SSH
git remote set-url origin git@github.com:YOUR_USERNAME/facebook-clone.git

# Now push
git push -u origin main
```

---

## Step 7: Verify on GitHub

1. Go to your GitHub repository page
2. Refresh the page
3. You should see all your files!
4. Click on README.md to see how it looks on GitHub

---

## Common Issues and Solutions

### Issue 1: "Permission denied"
```
Solution: Use Personal Access Token or set up SSH key (see Step 6)
```

### Issue 2: "Repository not found"
```bash
# Check your remote URL
git remote -v

# Update if wrong
git remote set-url origin https://github.com/YOUR_USERNAME/facebook-clone.git
```

### Issue 3: "Branch 'main' doesn't exist"
```bash
# If your branch is called 'master', use:
git push -u origin master

# Or rename to 'main':
git branch -M main
git push -u origin main
```

### Issue 4: "Updates were rejected"
```bash
# Someone else pushed changes, pull first:
git pull origin main --rebase
git push -u origin main
```

---

## Daily Git Workflow (After Initial Setup)

```bash
# 1. Start your day - get latest code
git pull origin main

# 2. Make changes to your code
# ... edit files ...

# 3. Check what changed
git status

# 4. Stage changes
git add .

# 5. Commit with message
git commit -m "feat: add new feature"

# 6. Push to GitHub
git push

# (No need for -u origin main anymore, just 'git push')
```

---

## Useful Git Commands

```bash
# View commit history
git log

# View last 5 commits, one line each
git log --oneline -n 5

# See what changed in files
git diff

# Undo changes to a file (before staging)
git checkout -- filename.txt

# Unstage a file (keep changes)
git reset HEAD filename.txt

# View all branches
git branch

# Create and switch to new branch
git checkout -b feature/new-feature

# Switch branches
git checkout main

# Delete a branch
git branch -d feature/old-feature
```

---

## Next Steps

After pushing to GitHub:

1. ✅ Add a nice profile picture to your GitHub account
2. ✅ Star your own repository (why not! 😄)
3. ✅ Share the repository link with friends or on your resume
4. ✅ Enable GitHub Pages to host documentation
5. ✅ Set up branch protection rules for main branch
6. ✅ Invite collaborators to your project

---

## GitHub Best Practices

### Do's ✅
- Commit often with meaningful messages
- Pull before you push
- Use branches for new features
- Write descriptive commit messages
- Keep commits small and focused

### Don'ts ❌
- Don't commit sensitive data (passwords, API keys)
- Don't commit large files (>100MB)
- Don't force push to shared branches
- Don't commit compiled code (add to .gitignore)
- Don't use vague commit messages like "update" or "fix"

---

## Protecting Sensitive Data

**Before committing, make sure you don't have:**
- ❌ Database passwords
- ❌ API keys
- ❌ AWS credentials
- ❌ Private keys

**Check these files:**
```bash
# View .gitignore
cat .gitignore

# Should include:
# *.env
# application.properties (if it has secrets)
# *.pem
# *.key
```

**If you accidentally committed secrets:**
```bash
# Remove from last commit (before pushing)
git reset HEAD~1
git add .
git commit -m "your message"

# If already pushed, change the secret immediately!
```

---

## 🎉 Congratulations!

Once you complete these steps, your code is now:
- ✅ Backed up on GitHub
- ✅ Version controlled
- ✅ Shareable with others
- ✅ Ready for collaboration
- ✅ Part of your portfolio!

**Your repository URL:**
```
https://github.com/YOUR_USERNAME/facebook-clone
```

Share this link on your resume, LinkedIn, or with potential employers!

---

## Learning Resources

- **GitHub Guides**: https://guides.github.com/
- **Git Documentation**: https://git-scm.com/doc
- **Interactive Git Tutorial**: https://learngitbranching.js.org/
- **GitHub Skills**: https://skills.github.com/

---

**Happy coding! 🚀**
