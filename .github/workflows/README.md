# GitHub Actions CI/CD Workflows

## Overview

This directory contains GitHub Actions workflows for automating build, test, and deployment processes.

## Current Workflows

### 1. `ci.yml` - Continuous Integration

**Triggers:**
- Push to `main`, `develop`, or any `feature/*` branch
- Pull requests to `main` or `develop`

**What it does:**
1. ✅ Checks out your code
2. ✅ Sets up Java 21
3. ✅ Starts PostgreSQL database for tests
4. ✅ Caches Maven dependencies (faster builds)
5. ✅ Builds the application
6. ✅ Runs all tests
7. ✅ Packages JAR file
8. ✅ Uploads JAR as downloadable artifact

**Build time:** ~3-5 minutes (first run), ~1-2 minutes (cached)

---

## Required GitHub Secrets

### Setup Instructions:
1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add these secrets:

### Required Secrets:

| Secret Name | Description | Example |
|------------|-------------|---------|
| `JWT_SECRET` | JWT signing secret (min 64 chars) | Generate with: `openssl rand -base64 64` |
| `SUPERADMIN_PASSWORD` | Super admin default password | `YourStrongPassword123!` |
| `SUPERADMIN_EMAIL` | Super admin email | `admin@yourdomain.com` |
| `MINIO_ACCESS_KEY` | MinIO access key for file storage | `minioadmin` (for dev) |
| `MINIO_SECRET_KEY` | MinIO secret key | `minioadmin123` (for dev) |
| `PAYSTACK_SECRET_KEY` | Paystack secret key | `sk_test_xxxxx` |
| `PAYSTACK_PUBLIC_KEY` | Paystack public key | `pk_test_xxxxx` |
| `PAYSTACK_WEBHOOK_SECRET` | Paystack webhook secret | `whsec_xxxxx` |

### Optional Secrets (have defaults):
- `DB_URL` - Database URL (defaults to test database)
- `DB_USER` - Database user (defaults to `postgres`)
- `DB_PASSWORD` - Database password (defaults to `postgres`)

---

## Environment Variables (Automatically Set)

These are set automatically in the workflow:
- `DB_URL=jdbc:postgresql://localhost:5432/mths_test`
- `DB_USER=postgres`
- `DB_PASSWORD=postgres`
- `MAIL_HOST=localhost`
- `MAIL_PORT=1025`
- `SERVER_PORT=8081`

---

## Viewing Build Results

### After pushing code:
1. Go to your repository on GitHub
2. Click the **Actions** tab
3. Click on your latest workflow run
4. See the results:
   - ✅ Green check = Success
   - ❌ Red X = Failed (click for logs)
   - 🟡 Yellow circle = Running

### Download JAR artifact:
1. Click on a successful workflow run
2. Scroll to **Artifacts** section at the bottom
3. Download `mths-backend-jar`

---

## Adding Build Status Badge to README

Add this to your `README.md`:

```markdown
![Build Status](https://github.com/YOUR_USERNAME/YOUR_REPO/workflows/MTHS%20Backend%20CI%2FCD/badge.svg)
```

Replace `YOUR_USERNAME` and `YOUR_REPO` with your actual values.

---

## Troubleshooting

### Build fails with "JWT_SECRET not set"
→ Add `JWT_SECRET` to GitHub Secrets (Settings → Secrets → Actions)

### Build fails with database connection error
→ PostgreSQL service is automatically started, check logs for specific error

### Tests fail but work locally
→ Ensure environment variables match between local and CI
→ Check if test database is properly initialized

### Build is slow
→ First build takes longer (downloads dependencies)
→ Subsequent builds are faster (cached dependencies)

---

## Customizing the Workflow

### Run only on specific branches:
```yaml
on:
  push:
    branches: [ main ]  # Only main branch
```

### Skip CI for documentation changes:
Add `[skip ci]` to your commit message:
```bash
git commit -m "Update docs [skip ci]"
```

### Add deployment step:
After tests pass, add deployment to cloud platform (Heroku, Railway, AWS, etc.)

---

## Best Practices

✅ **DO:**
- Run CI on every push
- Fix failing tests immediately
- Keep builds under 5 minutes
- Use secrets for sensitive data
- Cache dependencies

❌ **DON'T:**
- Commit secrets to code
- Skip tests to "speed up" builds
- Ignore failing CI
- Use production credentials in CI

---

## Support

For issues with GitHub Actions:
- Check workflow logs in Actions tab
- Review secret configuration
- Verify `pom.xml` is correct
- Check Java version compatibility

---

**Status:** ✅ Active and Running
**Last Updated:** December 2024
