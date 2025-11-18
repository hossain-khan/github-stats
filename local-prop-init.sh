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

# Copy the file and update only the main date configuration (not commented examples)
# This uses awk to skip the sample header, replace dates, and keep everything else
awk -v after="$ONE_MONTH_AGO" -v before="$TODAY" '
{
    # Skip the sample file header lines (first 3 lines)
    if (NR <= 3) {
        next
    }
    # Only replace the first occurrence of date_limit_after (not commented lines)
    if (!after_replaced && /^date_limit_after=/ && !/^#/) {
        print "date_limit_after=" after
        after_replaced = 1
        next
    }
    # Only replace the first occurrence of date_limit_before (not commented lines)
    if (!before_replaced && /^date_limit_before=/ && !/^#/) {
        print "date_limit_before=" before
        before_replaced = 1
        next
    }
    # Print all other lines as-is
    print
}
' "$SAMPLE_FILE" > "$LOCAL_FILE"

echo "✓ local.properties created successfully!"
echo
echo "Note: Don't forget to update the following values in local.properties:"
echo "  - access_token (your GitHub personal access token)"
echo "  - repository_owner (GitHub org/user name)"
echo "  - repository_id (repository name)"
echo "  - authors (comma-separated list of GitHub user IDs)"
