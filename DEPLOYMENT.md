# Deployment Guide

This guide will help you deploy your Tetris 1984 game to either GitHub Pages or Vercel.

## Option 1: GitHub Pages Deployment

### Prerequisites
- A GitHub account
- Git installed on your computer

### Steps

1. **Initialize Git Repository** (if not already done):
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   ```

2. **Create GitHub Repository**:
   - Go to [GitHub](https://github.com) and create a new repository
   - Name it `tetris-1984` (or your preferred name)
   - Don't initialize with README, .gitignore, or license (since you already have files)

3. **Push to GitHub**:
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/tetris-1984.git
   git branch -M main
   git push -u origin main
   ```

4. **Enable GitHub Pages**:
   - Go to your repository on GitHub
   - Click on "Settings" tab
   - Scroll down to "Pages" section
   - Under "Source", select "Deploy from a branch"
   - Choose "gh-pages" branch
   - Click "Save"

5. **Automatic Deployment**:
   - The GitHub Actions workflow (`.github/workflows/deploy.yml`) will automatically deploy your site
   - Every time you push to the main branch, it will automatically deploy
   - Your site will be available at: `https://YOUR_USERNAME.github.io/tetris-1984`

## Option 2: Vercel Deployment

### Prerequisites
- A Vercel account (free at [vercel.com](https://vercel.com))
- Node.js installed (optional, but recommended)

### Steps

1. **Install Vercel CLI** (optional):
   ```bash
   npm install -g vercel
   ```

2. **Deploy via Vercel Dashboard** (Recommended):
   - Go to [vercel.com](https://vercel.com) and sign up/login
   - Click "New Project"
   - Import your GitHub repository
   - Vercel will automatically detect it's a static site
   - Click "Deploy"

3. **Deploy via CLI** (Alternative):
   ```bash
   vercel
   ```
   - Follow the prompts to link your project
   - Your site will be deployed automatically

4. **Custom Domain** (Optional):
   - In your Vercel dashboard, go to project settings
   - Add your custom domain if you have one

## Option 3: Netlify Deployment

### Steps

1. **Go to Netlify**:
   - Visit [netlify.com](https://netlify.com)
   - Sign up/login

2. **Deploy**:
   - Drag and drop your project folder to the deploy area
   - Or connect your GitHub repository
   - Netlify will automatically deploy your site

## File Structure for Deployment

Your project should have this structure:
```
tetris-1984/
├── index.html          # Main game file
├── style.css           # Styles
├── tetris.js           # Game logic
├── package.json        # Project metadata
├── vercel.json         # Vercel configuration
├── .github/
│   └── workflows/
│       └── deploy.yml  # GitHub Actions workflow
└── README.md           # Project documentation
```

## Post-Deployment

After deployment, your game will be accessible at:
- **GitHub Pages**: `https://YOUR_USERNAME.github.io/tetris-1984`
- **Vercel**: `https://tetris-1984-YOUR_USERNAME.vercel.app`
- **Netlify**: `https://your-project-name.netlify.app`

## Troubleshooting

### Common Issues

1. **Game not loading**: Check browser console for errors
2. **Assets not found**: Ensure all file paths are correct
3. **CORS issues**: Deploy to a proper hosting service (not file:// protocol)

### Testing Locally

Before deploying, test locally:
```bash
# Using Python
python -m http.server 8000

# Using Node.js
npx http-server

# Using PHP
php -S localhost:8000
```

Then visit `http://localhost:8000` in your browser.

## Continuous Deployment

Both GitHub Pages and Vercel support continuous deployment:
- Push changes to your main branch
- Your site will automatically update
- No manual deployment needed

## Performance Optimization

Your current setup is already optimized for web deployment:
- No external dependencies
- Minimal file sizes
- Fast loading times
- Mobile-responsive design 