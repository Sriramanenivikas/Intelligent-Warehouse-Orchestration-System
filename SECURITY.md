# Security

Security expectations for implementation:

- least-privilege IAM
- no secrets committed to the repository
- prefer GitHub OIDC to AWS over long-lived cloud credentials
- all service-to-service traffic treated as authenticated and authorized
- encryption in transit and at rest
- auditable access paths for production systems
- security review for public and partner APIs before release

Security reports should not be raised as public GitHub issues once the repository begins carrying real credentials, infrastructure identifiers, or production details.
