#!/bin/bash

# Script to initialize local.properties from local_sample.properties
# Automatically sets date_limit_after to one month ago and date_limit_before to today

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SAMPLE_FILE="$SCRIPT_DIR/local_sample.properties"
LOCAL_FILE="$SCRIPT_DIR/local.properties"

# Check if local_sample.properties exists
if [ ! -f "$SAMPLE_FILE" ]; then
    echo "Error: $SAMPLE_FILE not found!"
    exit 1
fi

# Check if local.properties already exists
if [ -f "$LOCAL_FILE" ]; then
    read -p "local.properties already exists. Overwrite? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 0
    fi
fi

# Calculate dates
# One month ago
ONE_MONTH_AGO=$(date -v-1m "+%Y-%m-%d" 2>/dev/null || date -d "1 month ago" "+%Y-%m-%d")
# Today
TODAY=$(date "+%Y-%m-%d")

echo "Creating local.properties with:"
echo "  date_limit_after = $ONE_MONTH_AGO"
echo "  date_limit_before = $TODAY"
echo

# Copy and update the file
sed -e "s/^date_limit_after=.*$/date_limit_after=$ONE_MONTH_AGO/" \
    -e "s/^date_limit_before=.*$/date_limit_before=$TODAY/" \
    -e "s/^#date_limit_after=.*$/date_limit_after=$ONE_MONTH_AGO/" \
    -e "s/^#date_limit_before=.*$/date_limit_before=$TODAY/" \
    "$SAMPLE_FILE" > "$LOCAL_FILE"

echo "✓ local.properties created successfully!"
echo
echo "Note: Don't forget to update the following values in local.properties:"
echo "  - access_token (your GitHub personal access token)"
echo "  - repository_owner (GitHub org/user name)"
echo "  - repository_id (repository name)"
echo "  - authors (comma-separated list of GitHub user IDs)"
