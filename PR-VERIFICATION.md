# Advancement evidence PR verification

This PR retains the provider's existing Java 21 / Paper 1.21.11 compilation baseline. CI now checks the exact PR head on every target branch, runs clean Maven verification without skipping tests, and preserves test reports. It does not deploy or release artifacts.

Original source change remains in feature/advancement-evidence. Existing player data must be retained. New local/hosted results will be recorded on the PR, not inferred from older test counts.

Fresh PR-cleanup local verification: {"tests":88,"failures":0,"errors":0,"skipped":0}. Clean Maven verify packaged the existing plugin version without gameplay changes. Hosted CI remains a separate check on the pushed head.

## Fresh review: world-reset history boundary

The initial follow-up review reproduced a migration bug: retained pre-reset analytics could recreate cleared advancement evidence on the next startup. A durable exclusive epoch-seconds cutoff is now stored in diaries.yml alongside lastWorldUid and applied after world-reset handling. Old and same-second ambiguous events are excluded only from advancement migration; analytics records themselves are retained. Live events still update dedicated evidence normally, including in the reset second. Version 1.4.11 contains this correction.

Regression evidence: diary-reset-red.log failed the missing persisted cutoff and filtered-summary cases on the old code. The final suite also checks startup ordering.

Final reset-fix verification: 91 Java tests, zero failures/errors/skips; clean verify packages DiaryKeeper 1.4.11. This is local evidence, not a deployment.
