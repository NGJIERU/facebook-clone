# 🤝 Contributing to Facebook Clone

Thank you for your interest in contributing to this project! This guide will help you get started with contributing, whether you're fixing a bug, adding a feature, or improving documentation.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Requirements](#testing-requirements)
- [Documentation](#documentation)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. Please be respectful and constructive in your interactions.

### Expected Behavior

- ✅ Use welcoming and inclusive language
- ✅ Be respectful of differing viewpoints
- ✅ Accept constructive criticism gracefully
- ✅ Focus on what's best for the community
- ✅ Show empathy towards others

### Unacceptable Behavior

- ❌ Harassment or discriminatory language
- ❌ Trolling or insulting comments
- ❌ Public or private harassment
- ❌ Publishing others' private information
- ❌ Unprofessional conduct

---

## Getting Started

### 1. Fork the Repository

```bash
# Click "Fork" button on GitHub
# Then clone your fork:
git clone https://github.com/YOUR_USERNAME/facebook-clone.git
cd facebook-clone
```

### 2. Add Upstream Remote

```bash
# Add the original repository as upstream
git remote add upstream https://github.com/ORIGINAL_OWNER/facebook-clone.git

# Verify remotes
git remote -v
# origin    https://github.com/YOUR_USERNAME/facebook-clone.git (fetch)
# origin    https://github.com/YOUR_USERNAME/facebook-clone.git (push)
# upstream  https://github.com/ORIGINAL_OWNER/facebook-clone.git (fetch)
# upstream  https://github.com/ORIGINAL_OWNER/facebook-clone.git (push)
```

### 3. Set Up Development Environment

```bash
# Follow the instructions in SETUP.md
# Ensure all tests pass before making changes
docker-compose up -d
cd frontend && npm install && cd ..
```

---

## Development Workflow

### 1. Create a Feature Branch

```bash
# Always start from the latest main branch
git checkout main
git pull upstream main

# Create a descriptive branch name
git checkout -b feature/add-comment-reactions
# or
git checkout -b fix/fix-login-timeout
# or
git checkout -b docs/improve-readme
```

### Branch Naming Convention

- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation changes
- `refactor/` - Code refactoring
- `test/` - Adding or updating tests
- `chore/` - Maintenance tasks

### 2. Make Your Changes

```bash
# Make changes to the code
# Test your changes locally

# For backend changes:
cd backend/auth-service
mvn test  # Run unit tests
docker-compose up -d --build auth-service  # Test in Docker

# For frontend changes:
cd frontend
npm run dev  # Test in browser
npm run build  # Ensure it builds
```

### 3. Keep Your Branch Updated

```bash
# Regularly sync with upstream
git fetch upstream
git rebase upstream/main

# Resolve any conflicts
# Continue development
```

---

## Coding Standards

### Java (Backend Services)

**Style Guide**: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

```java
// ✅ Good
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User findById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}

// ❌ Bad
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository; // Avoid field injection
    
    public User findById(UUID id) {
        return userRepository.findById(id).get(); // Unsafe
    }
}
```

**Key Points:**
- Use constructor injection, not field injection
- Follow SOLID principles
- Write meaningful variable names
- Add JavaDoc for public methods
- Use Optional for nullable returns
- Handle exceptions properly

### JavaScript/Vue.js (Frontend)

**Style Guide**: [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)

```javascript
// ✅ Good
export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))
  
  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials)
      user.value = response.user
      token.value = response.token
      localStorage.setItem('token', response.token)
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }
  
  return { user, token, login }
})

// ❌ Bad
export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))
  
  const login = (credentials) => {
    authService.login(credentials).then(response => {
      user.value = response.user
      token.value = response.token
      localStorage.setItem('token', response.token)
    })
  }
  
  return { user, token, login }
})
```

**Key Points:**
- Use `const` for variables that don't change
- Use arrow functions
- Use async/await over promises
- Destructure props and reactive objects
- Follow Vue 3 Composition API patterns
- Use TypeScript types where applicable

### GraphQL

```graphql
# ✅ Good - Descriptive names and proper structure
type User {
  id: ID!
  username: String!
  email: String!
  profile: UserProfile
  posts(limit: Int, offset: Int): [Post!]!
}

type Query {
  user(id: ID!): User
  users(filter: UserFilter, pagination: PaginationInput): UserConnection!
}

# ❌ Bad - Generic names and poor structure
type User {
  id: ID!
  data: String  # Too generic
  stuff: [Thing]  # Unclear
}
```

### SQL & Database

```sql
-- ✅ Good - Indexed, normalized, clear naming
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_profiles_username ON user_profiles(username);
CREATE INDEX idx_user_profiles_email ON user_profiles(email);

-- ❌ Bad - No indexes, poor naming
CREATE TABLE users (
    id VARCHAR(255),
    data TEXT
);
```

---

## Commit Guidelines

### Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code formatting (no functional changes)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements

### Examples

```bash
# Feature
git commit -m "feat(auth): add OAuth2 Google login support"

# Bug fix
git commit -m "fix(feed): resolve infinite scroll pagination issue"

# Documentation
git commit -m "docs(readme): add AWS deployment section"

# With body and footer
git commit -m "feat(notification): implement WebSocket push notifications

- Add STOMP WebSocket configuration
- Create notification event listeners
- Implement real-time notification delivery

Closes #123"
```

### Good Commit Messages

```
✅ feat(user): add profile picture upload functionality
✅ fix(auth): prevent token expiration edge case
✅ docs(api): update GraphQL schema documentation
✅ refactor(feed): extract feed ranking logic to service
✅ test(auth): add integration tests for JWT validation
```

### Bad Commit Messages

```
❌ update code
❌ fix bug
❌ changes
❌ asdfasdf
❌ WIP
```

---

## Pull Request Process

### 1. Before Submitting

**Checklist:**
- [ ] Code follows project coding standards
- [ ] All tests pass locally
- [ ] New tests added for new features
- [ ] Documentation updated (if needed)
- [ ] No unnecessary debug code or console logs
- [ ] Commit messages follow conventions
- [ ] Branch is up to date with main

### 2. Create Pull Request

```bash
# Push your branch to your fork
git push origin feature/add-comment-reactions

# Go to GitHub and click "New Pull Request"
# Select: base: main <- compare: feature/add-comment-reactions
```

### 3. PR Title and Description Template

```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Related Issues
Closes #123
Fixes #456

## Testing
Describe how you tested your changes:
- Unit tests: `mvn test`
- Integration tests: `mvn verify`
- Manual testing: Tested login flow with Google OAuth

## Screenshots (if applicable)
![Screenshot](url)

## Checklist
- [ ] My code follows the project's coding standards
- [ ] I have performed a self-review of my code
- [ ] I have commented my code where necessary
- [ ] I have updated the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing tests pass locally
- [ ] Any dependent changes have been merged

## Additional Notes
Any additional information for reviewers.
```

### 4. Code Review Process

- Maintainers will review your PR
- Address feedback and requested changes
- Push updates to the same branch
- Once approved, your PR will be merged

### 5. After Merge

```bash
# Switch to main and pull latest
git checkout main
git pull upstream main

# Delete your feature branch
git branch -d feature/add-comment-reactions
git push origin --delete feature/add-comment-reactions
```

---

## Testing Requirements

### Backend Tests

```bash
# Unit tests
cd backend/auth-service
mvn test

# Integration tests
mvn verify

# Code coverage (should be >80%)
mvn test jacoco:report
open target/site/jacoco/index.html
```

### Frontend Tests

```bash
cd frontend

# Unit tests
npm run test:unit

# E2E tests
npm run test:e2e

# Coverage
npm run test:coverage
```

### Test Guidelines

**Unit Tests:**
- Test individual functions/methods
- Mock external dependencies
- Cover edge cases and error scenarios
- Aim for >80% code coverage

**Integration Tests:**
- Test service interactions
- Use test containers for databases
- Verify API contracts
- Test GraphQL queries/mutations

**E2E Tests:**
- Test critical user flows
- Login → Create Post → View Feed
- User registration → Profile setup
- Error scenarios

---

## Documentation

### What to Document

1. **Code Comments**
   - Explain "why", not "what"
   - Document complex algorithms
   - Add JavaDoc/JSDoc for public APIs

2. **README Updates**
   - New features
   - Configuration changes
   - Breaking changes

3. **API Documentation**
   - GraphQL schema changes
   - REST endpoint changes
   - Request/response examples

4. **Architecture Docs**
   - System design changes
   - New microservices
   - Database schema changes

### Documentation Style

```java
/**
 * Validates and issues JWT access tokens for authenticated users.
 * 
 * This method performs the following:
 * 1. Validates user credentials against the database
 * 2. Checks if the user account is active
 * 3. Generates a JWT token with user claims
 * 4. Stores refresh token in Redis cache
 *
 * @param credentials User login credentials (username/password)
 * @return AuthResponse containing access token, refresh token, and user info
 * @throws AuthenticationException if credentials are invalid
 * @throws UserAccountLockedException if account is locked
 */
public AuthResponse authenticate(LoginCredentials credentials) {
    // Implementation
}
```

---

## Need Help?

- 💬 **Discussions**: [GitHub Discussions](https://github.com/YOUR_USERNAME/facebook-clone/discussions)
- 🐛 **Issues**: [GitHub Issues](https://github.com/YOUR_USERNAME/facebook-clone/issues)
- 📧 **Email**: dev@facebook-clone.com
- 📖 **Documentation**: [Wiki](https://github.com/YOUR_USERNAME/facebook-clone/wiki)

---

## Recognition

Contributors will be recognized in:
- README.md Contributors section
- GitHub Contributors page
- Release notes for significant contributions

---

**Thank you for contributing! 🎉**

Your contributions make this project better for everyone.
